# How to start using this repository (tested on Ubuntu 14, Ubuntu 16 and Debian stretch)

## Install Ansible

1. Install the proper Ansible version at local PC by running the bootstrap script:

```
bootstrap.sh
```

2. Enable Ansible for current shell session by running:

```
exec bash
```

## How to provision Checkstyle Jenkins instance with Ansible

### How to provision Jenkins, Nginx and common part (common part includes apt packages, pip packages, sshd settings, etc.):
```
ansible-playbook -i inventories/jenkins jenkins.yaml
```

### How to provision Jenkins only

```
ansible-playbook -i inventories/jenkins jenkins.yaml --tags jenkins
```

### How to provision Ngins only

```
ansible-playbook -i inventories/jenkins jenkins.yaml --tags nginx
```
