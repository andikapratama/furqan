require 'net/http'
require 'rexml/document'
require 'rubygems'
require 'sqlite3'

puts "Hello World"

file = File.open("quran-data.xml", "rb")
xml_data = file.read

db = SQLite3::Database.open "furqan.sqlite"

stmt = db.prepare( "update Surah set ArabicName = ?, Type = ? where Id=?" )

doc = REXML::Document.new(xml_data)
doc.elements.each('quran/suras/sura') do |ele|
	name = ele.attributes['tname']
	type = ele.attributes['type']
	index = ele.attributes['index']
	db.execute "update Surah set TransliterationName = ? where Id=?",name,index
	puts ele.attributes['tname']
end
