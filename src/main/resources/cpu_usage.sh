#
# This script is used for ctrl cpu usage
# Report bugs to wangtong.wt@alibaba-inc.com
#
#!/bin/bash

#catch signal 1 2 3 15
trap "exit_and_clean" 1 2 3 15

CPU_CORE_NUM=`cat /proc/cpuinfo |grep processor|wc -l`
CPU_USAGE_MAP=(`echo $1 | sed -s 's/,/ /g'`)
CPU_USAGE_UP_TO=
CPU_USAGE_LAST=0
ONE_PER_WORK_TIME=$((1000/100))
START_FLAG=999999
ONCE=$3
ADJUST_LIMIT=1
[ ! -z $2 ] && ADJUST_LIMIT=$2
WORK_TIME_FOR_EVERY_CORE=()
#Integral in PID
E_SUM=0
Ki=1.8
I_SEPARATION=0
IS_ADJUSTED=0

echo CPU_CORE_NUM ${CPU_CORE_NUM}
echo ONE_PER_WORK_TIME ${ONE_PER_WORK_TIME}
echo ADJUST_LIMIT ${ADJUST_LIMIT}
echo ONCE ${ONCE}

function exit_and_clean()
{
        echo exit $0.
        rm -rf pipeRCT* CPU_USAGE_CTRL
        kill -9 0
        exit 0
}

function do_work()
{
        WORK_TIME=0
        while(true)
        do
                START=`date +%s%N`
                END=`date +%s%N`
                DIFF=`expr $END - $START`

                read -a msg
                if [ "$msg" != "" ]; then
                        if [ ${msg%.*} -eq $START_FLAG ]; then
                                echo "core $1 ready to work."
                        else
                                if [ ${msg%.*} -lt 0 ]; then
                                        WORK_TIME=0
                                else
                                        WORK_TIME=${msg%.*}
                                fi
                                echo "set work time to $WORK_TIME."
                        fi
                fi
                while [ `expr $DIFF / 1000000` -le $WORK_TIME ]
                do
                        END=`date +%s%N`
                        DIFF=`expr $END - $START`
                done
                if [ $WORK_TIME -ne 0 ]; then
                        sleep .$((1000-$WORK_TIME))
                else
                        sleep 1
                fi
        done < pipeRCT$1
}

function do_adjust()
{
        cpu_idle=`sar -P ALL 1 1 | grep all |tail -1 | awk '{print $8}'`
        cpu_usage=$((100-${cpu_idle%.*}))
        if [ ${#WORK_TIME_FOR_EVERY_CORE[@]} -eq $CPU_CORE_NUM ]; then
                last_usage=0
                for i in ${WORK_TIME_FOR_EVERY_CORE[*]}; do
                       last_usage=$(($last_usage+$i))
                done
                echo last_usage $last_usage
                cpu_usage=$((100-${cpu_idle%.*}-last_usage/$CPU_CORE_NUM))
        fi

        echo CPU usage before adjust: $cpu_usage
        if [ $cpu_usage -gt $CPU_USAGE_UP_TO ]; then
                echo "target is smaller than real, clear."
                unset WORK_TIME_FOR_EVERY_CORE
                clear
                return
        fi

        over=0
        core=0
        core_idle_list=`sar -P ALL 1 1 |tail -${CPU_CORE_NUM} | awk '{print $8}'`
        for idle in $core_idle_list; do
                one_core_use=$((100-${idle%.*}))
                if [ ${#WORK_TIME_FOR_EVERY_CORE[@]} -eq $CPU_CORE_NUM ]; then
                        one_core_use=$(($one_core_use-${WORK_TIME_FOR_EVERY_CORE[core]}))
                fi
                if [ $one_core_use -gt $CPU_USAGE_UP_TO ]; then
                        over=$(($over+($one_core_use-$CPU_USAGE_UP_TO)/$CPU_CORE_NUM))
                fi
                core=$(($core+1))
        done

        real_up=$(($CPU_USAGE_UP_TO-$over))
        echo real_up $real_up

        core=0
        for idle in $core_idle_list; do
                one_core_use=$((100-${idle%.*}))
                if [ ${#WORK_TIME_FOR_EVERY_CORE[@]} -eq $CPU_CORE_NUM ]; then
                        one_core_use=$(($one_core_use-${WORK_TIME_FOR_EVERY_CORE[core]}))
                fi
                echo one_core_use $one_core_use
                if [ $one_core_use -lt $CPU_USAGE_UP_TO ]; then
                        usage=$((($real_up-$one_core_use)*$ONE_PER_WORK_TIME))
                        WORK_TIME_FOR_EVERY_CORE[core]=$((${usage%.*}/$ONE_PER_WORK_TIME))
                        echo one_core_use $one_core_use
                        echo $usage > pipeRCT$core
                else
                        echo 0 > pipeRCT$core
                fi
                core=$(($core+1))
        done
        IS_ADJUSTED=$(($IS_ADJUSTED+1))
}

function fast_adjust()
{
        core=0
        for i in ${WORK_TIME_FOR_EVERY_CORE[*]}; do
            integral=`echo "scale=1;$E_SUM/$Ki*$ONE_PER_WORK_TIME" | bc`
            integral_work_time=${integral%.*}
            usage=$(($i*$ONE_PER_WORK_TIME + $integral_work_time))
            echo fast adjust core $core use $one_core_use
            echo $usage > pipeRCT$core
            core=$(($core+1))
        done
}

function make_pipe()
{
        rm -rf CPU_USAGE_CTRL
        mknod CPU_USAGE_CTRL p
        for ((i = 0; i < ${CPU_CORE_NUM}; i++))
        {
                rm -rf pipeRCT$i
                mknod pipeRCT$i p
        }
}

function check_args()
{
        rm -rf $0
        echo check args
        if [ $# -lt 1 ]; then
                echo "args is less then 1, exit."
                exit -1
        fi
        # cpu_idle=`sar -P ALL 1 1 | grep all |tail -1 | awk '{print $8}'`
        # cpu_usage=$((100-${cpu_idle%.*}))
        # echo cpu_usage $cpu_usage

        # for i in ${CPU_USAGE_MAP[@]:0}; do
        #         echo $i | while IFS=: read CPU_USAGE_UP_TO IGNORE ; do
        #                 if [ $cpu_usage -gt $CPU_USAGE_UP_TO ]; then
        #                         echo "target is smaller than real, exit."
        #                         exit -1
        #                 fi
        #         done;
        # done
}

function start()
{
        for ((i = 0; i < ${CPU_CORE_NUM}; i++))
        {
                do_work $i &
                taskset -cp $i $!
                echo $START_FLAG > pipeRCT$i
        }
}

function check_cpu_usage()
{
        cpu_idle=`sar -P ALL 1 1 | grep all |tail -1 | awk '{print $8}'`
        cpu_usage=$((100-${cpu_idle%.*}))
        echo cpu_usage $cpu_usage
        #adjust when cpu usage is less then target
        if [ $cpu_usage -ne $CPU_USAGE_UP_TO ]; then
                diff=$(($CPU_USAGE_UP_TO-$cpu_usage))
                if [ $IS_ADJUSTED -eq 1 ]; then
                    I_SEPARATION=$ADJUST_LIMIT
                    [ $diff -gt 0 ] && [ $diff -lt $ADJUST_LIMIT ] && ADJUST_LIMIT=$diff
                fi
                if [ ${diff#-} -ge $ADJUST_LIMIT ]; then
                    if [ ${diff#-} -le $I_SEPARATION ]; then
                        E_SUM=$(($E_SUM+$diff))
                        echo E_SUM $E_SUM
                        echo Ki $Ki
                        fast_adjust
                    else
                        E_SUM=0
                        IS_ADJUSTED=0
                        if [ $diff -gt 0 ]; then
                            do_adjust
                        fi
                    fi
                fi
        fi
}

function clear()
{
        E_SUM=0
        IS_ADJUSTED=0
        for ((i = 0; i < ${CPU_CORE_NUM}; i++))
        {
                echo 0 > pipeRCT$i
        }
}

function adjust_monitor()
{
        echo $START_FLAG > CPU_USAGE_CTRL &
        index=0
        sleep 3
        START_UP=`date +%s%N`
        while true; do
                sleep 1
                read msg
                if [ "$msg" = "" ]; then
                        END=`date +%s%N`
                        TOTAL_WORK_TIME=`expr $END - $START_UP`
                        SEC=`expr $TOTAL_WORK_TIME / 1000000000`
                        echo SEC $SEC
                        if [ $SEC -le $CPU_USAGE_LAST ]; then
                                check_cpu_usage
                        elif [ $SEC -gt $CPU_USAGE_LAST ];then
                                if [ $index -eq ${#CPU_USAGE_MAP[@]} ]; then
                                        clear
                                        unset WORK_TIME_FOR_EVERY_CORE
                                        echo $ONCE
                                        if [ "$ONCE" = "once" ]; then
                                                exit_and_clean
                                        fi
                                        continue
                                fi
                                cpu_usage=${CPU_USAGE_MAP[index]%:*}
                                last_time=${CPU_USAGE_MAP[index]#*:}
                                CPU_USAGE_UP_TO=$cpu_usage
                                CPU_USAGE_LAST=$(($last_time*60))
                                echo CPU_USAGE_UP_TO $CPU_USAGE_UP_TO
                                echo CPU_USAGE_LAST $CPU_USAGE_LAST
                                START_UP=`date +%s%N`
                                do_adjust
                                index=$((index+1))
                        fi
                elif [ "$msg" != "" ] && [ "$msg" != "$START_FLAG" ]; then
                        echo "New map catched, map: $msg"
                        CPU_USAGE_MAP=(`echo $msg | sed -s 's/,/ /g'`)
                        CPU_USAGE_LAST=0
                        index=0
                fi
        done < CPU_USAGE_CTRL
}

#MAIN
check_args "$@"
make_pipe
start
adjust_monitor


