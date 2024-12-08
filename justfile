


build:
    mvn clean package

bench: build
    java -jar target/benchmarks.jar "Pregen" -wi 2 -i 3 -f 1 -t 2