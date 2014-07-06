class datacore::java::java(

) inherits datacore::params {

    exec {
      "Get Java":
        command => "/usr/bin/wget --no-check-certificate --no-cookies --header \"Cookie: oraclelicense=accept-securebackup-cookie\" http://download.oracle.com/otn-pub/java/jdk/7u55-b13/jdk-7u55-linux-x64.tar.gz -O /tmp/jdk-7-linux-x64.tar.gz",
        timeout => 0;
      "Create dir":
        command => "/bin/mkdir -p /usr/lib/jvm";
      "Untar java":
        command => "/bin/tar xfz /tmp/jdk-7-linux-x64.tar.gz -C /usr/lib/jvm",
        timeout => 0,
        require => Exec['Get Java', 'Create dir'];
      "Rename java dir":
        command => "/bin/mv -f /usr/lib/jvm/jdk1.7.0_55 /usr/lib/jvm/jdk1.7.0",
        require => Exec['Untar java'],
        unless  => "/bin/ls /usr/lib/jvm/jdk1.7.0 | /bin/grep -c bin";
      "Configure java":
        command => "/usr/sbin/update-alternatives --install /usr/bin/java java /usr/lib/jvm/jdk1.7.0/bin/java 2000",
        require => Exec['Rename java dir'];
      "Configure javac":
        command => "/usr/sbin/update-alternatives --install /usr/bin/javac javac /usr/lib/jvm/jdk1.7.0/bin/javac 2000",
        require => Exec['Rename java dir'];
      "Configure javaws":
        command => "/usr/sbin/update-alternatives --install /usr/bin/javaws javaws /usr/lib/jvm/jdk1.7.0/bin/javaws 2000",
        require => Exec['Rename java dir'];
    }

}