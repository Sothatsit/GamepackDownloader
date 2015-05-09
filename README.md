# GamepackDownloader
Downloads the latest oldschool runescape gamepack.

# Usage
java -jar [jar-file-location] [options] [output-directory] [fernflower-options]

Options:

"-d" - download from runescape.com

"-s" - decompile

"-ds" - download from runescape.com and decompile if new version

# Output
The program will output files called "gamepack <version>.jar". The versions are calculated from the known versions previously downloaded.

So, to start off with it will be gamepack 1.jar and as new versions are downloaded it will become gamepack 2.jar, gamepack 3.jar, gamepack 4.jar, etc...
