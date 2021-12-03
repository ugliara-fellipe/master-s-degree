## Applications

These programs are compiled using the Cyan compiler, it can be found in the folder [project/jar](https://github.com/ugliara-fellipe/master-s-degree/tree/main/project/jar/) with the name of [saci.jar](https://github.com/ugliara-fellipe/master-s-degree/tree/main/project/jar/saci.jar).

To simplify this task, scripts were created to perform each necessary step in the compilation. Remembering that these scripts were used on a computer with an Ubuntu operating system, and must be executed from that folder. Compilation steps:

- deps.sh: must be the first script executed, it will install Sdk Java;
- config.h: this script creates the necessary folders to run the programs and copies the .class metaobjects files to a folder with the standard cyan libraries.

These two steps do not need to be repeated between compilations of programs, only if the cyan compiler will rebuild.

- build.sh: this script allows the compilation of the specified Cyan program, it receives the name of the folder with the program to be compiled;
- run.sh: this script could be called after the program was compiled, it receives as arguments: the name of the program that will be executed; the number of the instance associated with that execution; and the parameters that will be used by the program.
