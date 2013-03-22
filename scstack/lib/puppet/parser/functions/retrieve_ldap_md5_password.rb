# Returns md5 ldap password
require 'digest/md5'
require 'base64'

module Puppet::Parser::Functions
  newfunction(:retrive_ldap_md5_password, :type => :rvalue, :doc => <<-EOS
    Returns md5 ldap password.
    EOS
    ) do |args|
    '{MD5}' + Base64.encode64(Digest::MD5.digest(lookupvar('sadminpass'))).chomp!
  end
end