# alternative-metrics

### Dependencies (Python 3)
```
sudo apt install python3-pip
pip install --upgrade pip
python3 -m pip install --user numpy scipy matplotlib ipython jupyter pandas sympy nose
sudo apt-get install python3-tk
```
To generate the plots and csv tables use the following commands:
### Pearson correlation

```
python3 Main.py correlation-matrix $(cat sw_datasets.txt)
```

```
for d in $(cat sw_datasets.txt); 
    do echo $d; 
    python3 Main.py correlation-scatter $d; 
    done;
```

### Unavoidable movement

```
python3 Main.py unavoidable-matrix $(cat sw_datasets.txt)
```

```
for d in $(cat sw_datasets.txt); 
    do echo $d; 
    python3 Main.py unavoidable-boxplots $d; 
    done;
```

```    
python3 Main.py unavoidable-envelope
```

### Delta vis and delta data combinations

```
python3 Main.py delta-ratio-matrix $(cat sw_datasets.txt)
```

```
for d in $(cat sw_datasets.txt); 
    do echo $d; 
    python3 Main.py delta-ratio-boxplots $d; 
    done;
```

```
python3 Main.py delta-diff-matrix $(cat sw_datasets.txt)
```

```
for d in $(cat sw_datasets.txt); 
    do echo $d; 
    python3 Main.py delta-diff-boxplots $d; 
    done;
```

### Aspect Ratios
```
python3 Main.py ar-matrix $(cat sw_datasets.txt)
```

```
for d in $(cat sw_datasets.txt); 
    do echo $d; 
    python3 Main.py ar-boxplots $d; 
    done;
```

### Mean of 3 stability metrics
```
for d in $(cat sw_datasets.txt); 
    do echo $d; 
    python3 Main.py mean-boxplots $d; 
    done;
```

### Rank table
```
python3 Main.py rank-table plots/correlation-matrix/pearson.csv plots/delta-diff-matrix/deltadiff.csv plots/delta-ratio-matrix/deltaratio.csv plots/unavoidable-matrix/unavoidable.csv
python3 Main.py rank-table plots/ar-matrix/aspectratio.csv
```