# GamepackDownloader
Downloads the latest oldschool runescape gamepack.

# Usage
java -jar [jar-file-location] [options] [output-directory] [fernflower-options]

Options:

"d" - download gamepack

"s" - decompile latest version

"r" - refactor latest version

Example Usage:

"-d" - just downloads gamepack, useful to check for new versions
"-dr" - downloads and if new refactors gamepack but does not decompile
"-dsr" - downloads gamepack, if new refactors gamepack and then decompiles
"-sr" - refactors and decompiles the latest downloaded gamepack

# Output
The program will output files called "gamepack <version>.jar". The versions are calculated from the known versions previously downloaded.

So, to start off with it will be gamepack 1.jar and as new versions are downloaded it will become gamepack 2.jar, gamepack 3.jar, gamepack 4.jar, etc...
