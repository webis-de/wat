#!/bin/bash
cp ../demo-project.zip .
cp ../wat.jar .

is_in_docker_group=$(groups | sed 's/ /\n/g' | grep '^docker$' | wc -l)
if [ $is_in_docker_group -eq 0 ];then
  sudo docker build -t wat .
else
  docker build -t wat .
fi
