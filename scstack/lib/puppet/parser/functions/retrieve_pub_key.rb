# Returns content of ssh key pub file
module Puppet::Parser::Functions
  newfunction(:retrive_pub_key, :type => :rvalue, :doc => <<-EOS
    Returns content of ssh key pub file.
    EOS
  ) do |args|
    File.read(args[0]) if File::exists?(args[0])
  end
end