#!/bin/bash

#
# description: Starts and stops the App.
# author:wanglei

SERVER_NAME=garbageproject-0.0.1-SNAPSHOT.jar
LOG_PATH=garbage.log
USER=root
pid=0
start(){
  checkPid
  if [ ! -n "$pid" ]; then
    nohup java -jar $SERVER_NAME --Dspring.config.location=/usr/local/garbage/config/application.yml,application.properties  > $LOG_PATH 2>&1 &
    echo "--------------------------------------------------------------------------------------------------------------------------------------------"
	echo ""
    echo "                                                  启动完成，按CTRL+C退出日志界面即可                                                        "
	echo ""
    echo "--------------------------------------------------------------------------------------------------------------------------------------------"
    sleep 2s
    tail -f $LOG_PATH
  else
      echo "$SERVER_NAME is running PID: $pid"
  fi
}


status(){
   checkPid
   if [ ! -n "$pid" ]; then
     echo "$SERVER_NAME is not running"
   else
     echo "$SERVER_NAME is running, PID: $pid"
   fi 
}

checkPid(){
    pid=`ps -ef |grep $JAR_FILE |grep -v grep |awk '{print $2}'`
}

stop(){
    checkPid
    if [ ! -n "$pid" ]; then
     echo "$SERVER_NAME is not running"
    else
      echo "$SERVER_NAME stop..."
      kill -9 $pid
	  echo "$SERVER_NAME stop success"
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