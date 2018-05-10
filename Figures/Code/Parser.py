import glob
import pandas as pd
import re
import os

import Globals


def parse_rectangles(technique_id, dataset_id):
    path = Globals.rectangle_dir + '/' + technique_id + '/' + dataset_id
    files = [filename for filename in glob.iglob(path + '**/*.rect', recursive=True)]
    files = natural_sort(files)
    # Read each file into a dataframe
    dfs = [pd.read_csv(file, names=['id', 'x', 'y', 'w', 'h'], index_col='id') for file in files]
    return dfs


def natural_sort(l):
    convert = lambda text: int(text) if text.isdigit() else text.lower()
    alphanum_key = lambda key: [convert(c) for c in re.split('([0-9]+)', key)]
    return sorted(l, key=alphanum_key)


def list_techniques():
    list = natural_sort(os.listdir(Globals.rectangle_dir))
    if 'IncrementalLayoutWithMoves' in list:
        list.remove('IncrementalLayoutWithMoves')
    if 'IncrementalLayoutWithoutMoves' in list:
        list.remove('IncrementalLayoutWithoutMoves')
    return list
