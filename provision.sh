#!/bin/sh

cd /home/core/share
OLD_CONTAINER=$(docker ps |grep k2nr/dokkaa-builder | awk '{print $1}')
if [[ -n $OLD_CONTAINER ]]; then
    sudo docker kill $OLD_CONTAINER
fi
sudo docker build -t k2nr/dokkaa-builder . > /dev/null
HOST_IP=$(ip -o -4 addr show docker0 | awk -F '[ /]+' '/global/ {print $4}')
echo HOST_IP=$HOST_IP
sudo docker run -e HOST_IP=$HOST_IP -p 80:8080 -d k2nr/dokkaa-builder
