begin
  require 'sshkey'
rescue LoadError
  puts "sshkey was not found, waiting puppet to install"
end

# Returns content of ssh key pub file
# function_retrive_pub_key(["/opt/ssh-keys/gerritadmin_rsa.pub"])
module Puppet::Parser::Functions
  newfunction(:retrieve_pub_key, :type => :rvalue, :doc => <<-EOS
    Returns content of ssh key pub file.
    EOS
  ) do |args|

    # /usr/bin/ssh-keygen -t rsa -C 'gerritadmin@$domain' -N '' -f /opt/ssh-keys
    puts "Reading: " + lookupvar('gerritpublocation') + " file."
    if File::exists?(lookupvar('gerritpublocation'))
      k = SSHKey.new(File.read(lookupvar('gerritpublocation')))
      puts k.ssh_public_key
      return k.ssh_public_key
    end
  end
end