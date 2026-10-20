all: build

build:
	mkdir -p build
	javac -encoding UTF-8 -sourcepath src -d build src/bomberman/Main.java

run: build
	java -cp build bomberman.Main

clean:
	rm -rf build save.dat

.PHONY: all build run clean
