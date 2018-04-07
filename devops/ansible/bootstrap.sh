#!/bin/bash -e
# Usage: bootstrap.sh <ansible_version>
# - Trying to install pip 
# - Trying to install virtualenv
# - Creating virtualenv for target ansible version in $SAVE_PATH
# - Entering to virtualenv
# - Trying to install ansible and libs 
# - Saving source with new ansible to ~/.bashrc 


SAVE_PATH=~/.checkstyle/
LIBS='boto3'

# default version of 
TARGET_ANSIBLE_VERSION=latest

echo_green(){
	echo -e "\033[0;32m$@\033[0m"
}
echo_red(){
	echo -e "\033[0;31m$@\033[0m"
}
echo_lgreen(){
	echo -e "\033[1;32m$@\033[0m"
}
echo_yellow(){
	echo -e "\033[1;33m$@\033[0m"
}
echo_help(){
	cat <<EOF
Usage: $(basename $0) [ OPTION ] [ ansible_version ]
Options:
--not-persistent | Do not save version in environment
--remove-source  | Remove autoload from environment
EOF
}
echo_instructions(){
	COMMAND="exec bash"
	[[ ! -z $ANSIBLE_NOT_PERSISTENT ]] && COMMAND="VIRTUAL_ENV_DISABLE_PROMPT=1 source $SAVE_PATH/ansible_$CURRENT_ANSIBLE_VERSION/bin/activate"
	echo_lgreen To load ansible==$CURRENT_ANSIBLE_VERSION run command:
	echo_yellow $COMMAND
}

exist() {
	type $1 > /dev/null 2>&1
}
mutate_version(){
	local a=$1
	[[ $a == "" ]] || [[ $a == latest ]] && echo $a && return
	while [[ ${#a} -lt 7 ]]
	do
		a+=".0"
	done
	echo $a
}
exec_and_echo(){
	echo $@
	$@ && echo_green DONE && return 0
	echo_red ERROR
	return 1
}
rollback(){
	echo_green ROLLBACK
	exec_and_echo rm -rf $SAVE_PATH/ansible_$TARGET_ANSIBLE_VERSION
	exit 1
}
save_to_bashrc(){
	str="VIRTUAL_ENV_DISABLE_PROMPT=1 source $SAVE_PATH/ansible_$CURRENT_ANSIBLE_VERSION/bin/activate #checkstyle_ansible"
	remove_from_bashrc
	echo $str >> ~/.bashrc
	echo_lgreen Ansible version saved in your environment. To cancel it run:
	echo_yellow "$0 --remove-source"
}
remove_from_bashrc(){
	exec_and_echo sed -i '/#checkstyle_ansible/d' ~/.bashrc
}
post_install(){
	[[ -z $ANSIBLE_NOT_PERSISTENT ]] && save_to_bashrc || remove_from_bashrc
	echo_instructions
}

# args parser
while (($#))
do
	arg=$1
	case $arg in
		# remove persistent source
		--remove-source|-rs)
			remove_from_bashrc
			exit 0
		;;
		# remove persistent source
		--not-persistent|-np)
			ANSIBLE_NOT_PERSISTENT=1
		;;
		# mini help
		help|'?'|h|--help|-h)
			echo_help
			exit 0
		;;
		# version of ansible
		*)
			TARGET_ANSIBLE_VERSION=$arg
		;;
	esac
	shift
done

# parsing TARGET_ANSIBLE_VERSION - ansible version X.X.X.X or X.X.X or X.X or latest
TARGET_ANSIBLE_VERSION=$( mutate_version $TARGET_ANSIBLE_VERSION )
if [[ ! $TARGET_ANSIBLE_VERSION =~ ^[0-9]{1,2}.[0-9]{1,2}.[0-9]{1,2}.[0-9]{1,2}$ ]] && [[ $TARGET_ANSIBLE_VERSION != latest ]]
then
	echo_red Wrong arg format
	echo_help
	exit 1
fi

echo_green TARGET_ANSIBLE_VERSION $TARGET_ANSIBLE_VERSION
echo_green LIBS $LIBS

# disable virtualenv
exist deactivate && exec_and_echo deactivate && echo_green previous virtualenv deactivated

[[ ${SAVE_PATH:${#SAVE_PATH}-1} == '/' ]] && SAVE_PATH=${SAVE_PATH::${#SAVE_PATH}-1}

# Installation of pip
if ! exist pip 
then
	MNGR=$(
		( exist apt && echo apt ) ||
		( exist yum && echo yum ) ||
		( exist apt-get && echo apt-get )
	)
	# installation from repo
	if [[ ! -z $MNGR ]]
	then
		echo_green Detected $MNGR manager
		exec_and_echo $( [[ $UID != 0 ]] && echo sudo ) $MNGR -y -q install curl python-pip
	fi
	# installation by external script
	if ! exist pip && exist curl
	then
		exec_and_echo curl --silent "https://bootstrap.pypa.io/get-pip.py" -o "get-pip.py"
		exec_and_echo $( [[ $UID != 0 ]] && echo sudo ) python get-pip.py
		exec_and_echo rm -f get-pip.py
	fi
	# no other ways =(
	if ! exist pip
	then
		echo_red Can not install pip
		exit 1
	fi
fi

# virtualenv installation
if [[ ! -f ~/.local/bin/virtualenv ]]
then
	echo_green virtualenv installation
	exec_and_echo pip -q install --user virtualenv
	if [[ ! -f ~/.local/bin/virtualenv ]]
	then
		echo_red Can not install virtualenv
		exit 1
	fi
fi

# try to get latest version number
TARGET_LATEST=0
if [[ $TARGET_ANSIBLE_VERSION == latest ]]
then
	TARGET_LATEST=1
	TARGET_ANSIBLE_VERSION=$( pip search ansible | grep '^ansible ' | head -n1 | sed s/[^0-9\.]//g )
	TARGET_ANSIBLE_VERSION=$( mutate_version $TARGET_ANSIBLE_VERSION )
fi

# all files puts in dir $SAVE_PATH 
[[ ! -d $SAVE_PATH ]] && exec_and_echo mkdir $SAVE_PATH

# enter to virtualenv
exec_and_echo ~/.local/bin/virtualenv -q $SAVE_PATH/ansible_$TARGET_ANSIBLE_VERSION || rollback
exec_and_echo source $SAVE_PATH/ansible_$TARGET_ANSIBLE_VERSION/bin/activate

# check if needed ansible is already installed 
CURRENT_ANSIBLE_VERSION=Unknown
exist ansible && CURRENT_ANSIBLE_VERSION=$( ansible --version | head -1 | cut -d' ' -f 2 )
CURRENT_ANSIBLE_VERSION=$( mutate_version $CURRENT_ANSIBLE_VERSION )

if [[ $CURRENT_ANSIBLE_VERSION == $TARGET_ANSIBLE_VERSION ]] &&
	[[ -f $SAVE_PATH/ansible_$TARGET_ANSIBLE_VERSION/.libs_list ]] &&
	[[ $( cat $SAVE_PATH/ansible_$TARGET_ANSIBLE_VERSION/.libs_list ) == $LIBS ]]
then
	post_install
	exit 0
fi

# install ansible and libs
exec_and_echo pip -q install ansible==$TARGET_ANSIBLE_VERSION $LIBS || rollback
echo $LIBS > $SAVE_PATH/ansible_$TARGET_ANSIBLE_VERSION/.libs_list

# check installation 
CURRENT_ANSIBLE_VERSION=$( exist ansible && ansible --version | head -1 | cut -d' ' -f 2 )
CURRENT_ANSIBLE_VERSION=$( mutate_version $CURRENT_ANSIBLE_VERSION )

[[ $CURRENT_ANSIBLE_VERSION != $TARGET_ANSIBLE_VERSION ]] && rollback

post_install
exit 0
