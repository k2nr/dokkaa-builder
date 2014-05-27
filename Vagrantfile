# -*- mode: ruby -*-
# vi: set ft=ruby :

$provision = <<SCRIPT
sudo sh -c "wget -qO- https://get.docker.io/gpg | apt-key add -"
sudo sh -c "echo deb http://get.docker.io/ubuntu docker main\ > /etc/apt/sources.list.d/docker.list"
sudo apt-get update
sudo apt-get install -y git lxc-docker
sudo sh -c "echo DOCKER_OPTS=\\\\\\\"-H tcp://0.0.0.0:4243 -H unix:///var/run/docker.sock\\\\\\\" > /etc/default/docker"
sudo service docker restart
SCRIPT

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.define :dokkaa do |dokkaa|
    dokkaa.vm.box = "ubuntu/trusty64"
    dokkaa.vm.network "private_network", ip: "192.168.33.33"
    dokkaa.vm.provider :virtualbox do |v|
      v.memory = 1024
    end
    dokkaa.vm.synced_folder ".", "/var/lib/dokkaa", owner: "vagrant", group: "vagrant"
    dokkaa.vm.provision :shell, :inline => $provision
  end
end
