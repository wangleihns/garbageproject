#!/bin/bash

#
# description: Starts and stops the App.
# author:wanglei

SRVER_NAME=demo-0.0.1-SNAPSHOT.jar
LOG_PATH=smartHome.log
USER=root
pid=0
start(){
  checkpid
  if [ ! -n "$pid" ]; then
    JAVA_CMD="nohup java -jar $SRVER_NAME --Dspring.config.location=application.yml  > $LOG_PATH 2>&1 &"
    su - root -c "$JAVA_CMD"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------"
	echo ""
    echo "                                                  启动完成，按CTRL+C退出日志界面即可                                                        "
	echo ""
    echo "--------------------------------------------------------------------------------------------------------------------------------------------"
    sleep 2s
    tail -f $LOG_PATH
  else
      echo "$SRVER_NAME is runing PID: $pid"   
  fi
}


status(){
   checkpid
   if [ ! -n "$pid" ]; then
     echo "$SRVER_NAME is not runing"
   else
     echo "$SRVER_NAME is runing, PID: $pid"
   fi 
}

checkpid(){
    pid=`ps -ef |grep $JAR_FILE |grep -v grep |awk '{print $2}'`
}

stop(){
    checkpid
    if [ ! -n "$pid" ]; then
     echo "$SRVER_NAME is not runing"
    else
      echo "$SRVER_NAME stop..."
      kill -9 $pid
	  echo "$SRVER_NAME stop success"
    fi 
}

restart(){
    stop 
    sleep 2s
    start
}

case $1 in  
          start) start;;  
          stop)  stop;; 
          restart)  restart;;  
          status)  status;;   
              *)  echo "require start|stop|restart|status"  ;;  
esac 