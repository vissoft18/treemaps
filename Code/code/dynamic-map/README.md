# dynamic-treemap

Java implementation of treemapping techniques

To compile the code, from the root directory, run:

`mkdir bin; javac -d bin $(find . -name "*.java")`

To run the code that simply generates the rectangle series that can be used for analysis, run:

`java -cp ./bin com.ufrgs.Main technique_code input_dir width height output_dir`

If you'd like to see and interact with the generated treemap, add the frag `-v`. To advance a revision, press `x`, and to go back, press `z`;

`java -cp ./bin com.ufrgs.Main -v technique_code input_dir width height`

The codes for the techniques are:

Code     | Technique                        | Link to paper
---      | ---                              | ---
`snd`    | Slice and Dice                   | https://dl.acm.org/citation.cfm?id=115768
`sqr`    | Squarified Treemap               | https://link.springer.com/chapter/10.1007/978-3-7091-6783-0_4
`otpbm`  | Ordered Treemap Pivot-by-Middle  | https://dl.acm.org/citation.cfm?id=857710
`otpbs`  | Ordered Treemap Pivot-by-Size    | https://dl.acm.org/citation.cfm?id=857710
`strip`  | Strip Treemap                    | https://dl.acm.org/citation.cfm?id=857710
`nmac`   | Nmap Alternate Cut               | http://ieeexplore.ieee.org/document/6876012/
`nmew`   | Nmap Equal Weights               | http://ieeexplore.ieee.org/document/6876012/
`spiral` | Spiral Treemap                   | http://ieeexplore.ieee.org/document/4376152/



https://github.com/EduardoVernier/dynamic-map
version: commit 9a9773281c2a2c8eebb724b8567ecb39ad2c6968
