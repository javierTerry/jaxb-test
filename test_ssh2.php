<?php

$connection = ssh2_connect("172.19.209.75", 22);
if(ssh2_auth_password($connection, "root", "Mn$0c.l.")){
	echo "connection is authenticated \n";
	echo "";
} else {
	echo "failed! \n";
	echo "";
}

?>
