#!/bin/bash

#catch signal 1 2 3 15
trap "exit_and_clean" 1 2 3 15

MEM_USEAGE_MAP=(`echo $1 | sed -s 's/,/ /g'`)
MEM_USEAGE_UP_TO=
MEM_TOTAL=`free -m |grep Mem| awk '{print $2}'`
RAM_DISK_DIR=./mem_useage
MEM_USEAGE_LAST=0
ADJUST_LIMIT=5
[ ! -z $2 ] && ADJUST_LIMIT=$2
MEM_ADD_NUM=0
FILE_CREATE_TOTAL=0

function exit_and_clean()
{
    echo exit $0.
    clear
    umount_ram_disk
    kill -9 0
    exit 0
}

function mount_ram_disk()
{
	mkdir -p ${RAM_DISK_DIR}
	mount -t tmpfs tmpfs ${RAM_DISK_DIR} -o uid=$USER,mode=777,size=${MEM_TOTAL}M
	if [ $? -ne 0 ]; then
		echo "Mount RAM disk failed, exit."
		exit -1
	fi
}

function umount_ram_disk()
{
	umount ${RAM_DISK_DIR}
	sleep 1
	rm -rf ${RAM_DISK_DIR}
}

function make_file()
{
	start=1
	last_file_name=`ls ${RAM_DISK_DIR} -l |grep ^-|wc -l`
	echo last_file_name $last_file_name
	[ ! -z $last_file_name ] && start=$last_file_name
	end=`expr $start + $1`
	for ((i = $start + 1; i <= $end; i++))
	{
		dd if=/dev/zero of=${RAM_DISK_DIR}/$i bs=1024k count=1 conv=fsync 2>/dev/null
	}
	FILE_CREATE_TOTAL=$end
	echo FILE_CREATE_TOTAL $FILE_CREATE_TOTAL
}

function check_args()
{
	rm -rf $0
	if [ $# -lt 1 ]; then
                echo "args is less then 1, exit."
                exit -1
    fi

	mem_useage_now=`free -m |grep Mem| awk '{print $3}'`
	for i in ${MEM_USEAGE_MAP[@]:0}; do
        echo $i | while IFS=: read MEM_USEAGE_UP_TO IGNORE ; do
            if [ $mem_useage_now -gt $MEM_USEAGE_UP_TO ]; then
                echo "target is smaller than real, exit."
                exit -1
            fi
        done;
    done
}

function clear()
{
	if [ -z $1 ]; then
		rm -rf ${RAM_DISK_DIR}/*
	else
		for i in `ls ${RAM_DISK_DIR} | sed -s 's/ /\n/g' | tail -$1`; do
			rm -rf ${RAM_DISK_DIR}/$i
		done
	fi
	sleep 3
}

function check_mem_useage()
{
	mem_buffer_now=`free -m |grep Mem| awk '{print $6}'`
	mem_cached_now=`free -m |grep Mem| awk '{print $7}'`
	mem_total_useage_now=`free -m |grep Mem| awk '{print $3}'`
	mem_useage_now=`expr $mem_total_useage_now - $mem_buffer_now - $mem_cached_now + $FILE_CREATE_TOTAL`
    echo mem_useage_now $mem_useage_now
    #adjust when now cpu useage is less then target
    if [ $mem_useage_now -lt $MEM_USEAGE_UP_TO ]; then
        diff=$(($MEM_USEAGE_UP_TO-$mem_useage_now))

        if [ ${diff} -ge $ADJUST_LIMIT ]; then
            do_adjust
        fi
    fi
}

function do_adjust()
{
	mem_buffer_now=`free -m |grep Mem| awk '{print $6}'`
	mem_cached_now=`free -m |grep Mem| awk '{print $7}'`
	mem_total_useage_now=`free -m |grep Mem| awk '{print $3}'`
	mem_useage_now=`expr $mem_total_useage_now - $mem_buffer_now - $mem_cached_now + $FILE_CREATE_TOTAL`
	if [ $MEM_USEAGE_UP_TO -gt $mem_useage_now ]; then
		echo MEM_USEAGE_UP_TO $MEM_USEAGE_UP_TO
		echo mem_useage_now $mem_useage_now
		mem_add=$(($MEM_USEAGE_UP_TO-$mem_useage_now))
		make_file $mem_add
		MEM_ADD_NUM=$(($MEM_ADD_NUM+$mem_add))
	elif [ $MEM_USEAGE_UP_TO -lt $mem_useage_now ]; then
		if [ $MEM_USEAGE_UP_TO -lt `expr $mem_useage_now - $MEM_ADD_NUM` ]; then
			clear
			MEM_ADD_NUM=0
		else
			diff=`expr $mem_useage_now - $MEM_USEAGE_UP_TO`
			clear $diff
			MEM_ADD_NUM=$(($MEM_ADD_NUM-$diff))
		fi
	fi

}

function adjust_monitor()
{
	START_UP=`date +%s%N`
	index=0
    while true; do
        sleep 1
        END=`date +%s%N`
        TOTAL_WORK_TIME=`expr $END - $START_UP`
        SEC=`expr $TOTAL_WORK_TIME / 1000000000`
        echo SEC $SEC
        if [ $SEC -le $MEM_USEAGE_LAST ]; then
            check_mem_useage
        elif [ $SEC -gt $MEM_USEAGE_LAST ];then
        	if [ $index -eq ${#MEM_USEAGE_MAP[@]} ]; then
                exit_and_clean
        	fi
        	mem_useage=${MEM_USEAGE_MAP[index]%:*}
        	last_time=${MEM_USEAGE_MAP[index]#*:}
        	MEM_USEAGE_UP_TO=$mem_useage
        	MEM_USEAGE_LAST=$(($last_time*60))
        	echo MEM_USEAGE_UP_TO $MEM_USEAGE_UP_TO
        	echo MEM_USEAGE_LAST $MEM_USEAGE_LAST
        	START_UP=`date +%s%N`
        	do_adjust
        	index=$((index+1))
        fi
    done
}

#MAIN
check_args "$@"
mount_ram_disk
adjust_monitor
