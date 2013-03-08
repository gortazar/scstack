# Returns content of ssh key pub file
module Puppet::Parser::Functions
  newfunction(:retrive_pub_key, :type => :rvalue, :doc => <<-EOS
    Returns content of ssh key pub file.
    EOS
  ) do |args|
    File.read("file.rb") if File::exists?( "file.rb" )
  end
end