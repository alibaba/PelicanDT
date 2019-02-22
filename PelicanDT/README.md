PelicanDT（Pelican Distributed Test），是阿里云提供的一款 Linux 系统远程控制利器，是主要针对分布式应用提供的集成测试解决方案，用于帮助开发者简单、高效地测试分布式应用。PelicanDT 现已[开源](https://github.com/alibaba/PelicanDT)。

PelicanDT 具有以下特点：
- 使用 Java 语言与 Linux 系统交互。
- 本地控制 Linux 系统执行命令。
- 通过简单的操作对应用注入异常来模拟测试环境。例如：应用停服、CPU 过高、内存过高、网络中断、网络流量延时等环境。


基于 PelicanDT 实现的Demo：
- Dubbo测试Demo工程：[Dubbo-example](https://github.com/alibaba/PelicanDT/tree/master/Dubbo-example)
- Nacos测试Demo工程：[Nacos-example](https://github.com/alibaba/PelicanDT/tree/master/Nacos-example)
