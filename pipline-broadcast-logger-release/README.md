##
## Created by Feng Guangyuan, 7/11, 2016
##

Working topology:

Pipline : Client[send]---->DataNode1---->DataNode2---->DataNode3

                         DataNode1
                         /
                        /
Broadcast : Client[send]----DataNode2
                        \
                         \
                         DataNode3

1. Configurations for the workload.
    1> Cluster mode
    Setting '${WORKLOAD}/datanode1-conf/conf.txt'. You can add all the usable
datanodes informations into it, and meanwhile, make sure you have figure out
the current DataNode's name, like 'DataNode1' and the adjacent DataNode's name
like 'DataNode2'. If there was one name pointed, it means the current DataNode
is the last node to be started with no data to be transmitted to another node.
    2> Single node mode2> Single node mode
    Do 1>, then make sure that
    '${WORKLOAD}/datanode2-conf/conf.txt' is configured like the above.
    '${WORKLOAD}/datanode3-conf/conf.txt' is configured like the above.

[NOTE]: please guarantee '${WORKLOAD}/conf.txt' is a replication of 
        '${WORKLOAD}/datanode1-conf/conf.txt'.

2. How to compile and run?
    1> Compiling, please run ./compile.sh.

    2> Running a server process, please type commands on DataNodes:
        cd classes
        java DataNode

    3> Please type commands on a Client:
        ./compile.sh
        cd classes
        java Client
    After these steps, data transmitting will start.

More confusions, please feel free to contact to me.
