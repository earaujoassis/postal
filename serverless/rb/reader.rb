#!/usr/bin/env ruby

require 'aws-sdk'
require 'yaml'
require 'json'
require 'base64'
require 'mail'

config = YAML::load_file(File.join(__dir__, 'postal.yml'))
config = JSON.parse(JSON[config], symbolize_names: true)

ENV["AWS_ACCESS_KEY"] = config.dig :postal, :aws, :access_key
ENV["AWS_SECRET_ACCESS_KEY"] = config.dig :postal, :aws, :secret_access_key
ENV['AWS_REGION'] = config.dig :postal, :aws, :region
bucket = config.dig :postal, :bucket, :name
prefix = config.dig :postal, :bucket, :prefix
key = config.dig :postal, :key

kms = Aws::KMS::Client.new
encryption_client = Aws::S3::Encryption::Client.new(
  kms_key_id: key,
  kms_client: kms,
)

objects = encryption_client.client.list_objects_v2(bucket: bucket, prefix: prefix)
objects_contents = objects[:contents].sort_by{|h| h[:last_modified]}
puts "Messages from older to newer: \n"
objects_contents.each_with_index do |h, i|
  puts "#{i.to_s.rjust(2, "0")} #{h[:key]} (#{h[:last_modified]})"
end
print "\nWhich message you'd like to open: [0-#{objects_contents.length - 1}]: "
item_index = gets.chomp.to_i

item_key = objects_contents[item_index][:key]
resp = encryption_client.get_object(bucket: bucket, key: item_key)
message = resp.body.read
mail = Mail.new(message)
body = mail.body

if mail['Content-Transfer-Encoding'] == 'base64'
  body = Base64.decode64(body)
end

puts body
