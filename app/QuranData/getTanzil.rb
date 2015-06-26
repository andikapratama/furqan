#/usr/local/opt/ruby/bin/ruby
require 'open-uri'
require 'sqlite3'

puts "start"

file = File.open("tanzil.html", "rb")
page_content = file.read

#matches = /png"\/>(?<lang>\w+)<.+>(?<author>[\w\s]+)<\/td.+href="(?<link>.+)".+title="Download/.match page_content

matches = page_content.scan /png"\/>(?<lang>.+?)<.+?><.+?>(?<name>.+?)<.+?><.+?>(?<author>.*?)(&nbsp;<ahref="http.*?)?(<\/td).+?href="(?<link>.+?)".+?title="Download.+?title="Browse".+?href="(?<clink>.+?)" class="icon diff"/

db = SQLite3::Database.open "furqan.sqlite"

for m in matches do
	lang = m[0].force_encoding("UTF-8")
	name = m[1].force_encoding("UTF-8")
	author = m[2].force_encoding("UTF-8")
	link = m[3].force_encoding("UTF-8")
	clink = m[4].force_encoding("UTF-8")
	sourceId = -1
	db.execute( "select Id from Sources where DownloadLink = ?", link ) do |row|
		sourceId = row[0]
	end
#	db.execute "insert into Sources(Name,Language,LastModifiedDate,Type,Author,DownloadLink,UpdateLink) values(?,?,?,?,?,?,?)",name,lang,Time.now.to_s,"Translation",author,link,clink
	
	puts "#{sourceId} Language #{lang} : #{name}, by #{author}. link : #{link} update link #{clink}"
end

=begin
	lang = matches[:lang]
	author = matches[:author]
	link = matches[:link]
	puts "Language #{lang}, by #{author}. link : #{link}"

	puts "finish"
=end