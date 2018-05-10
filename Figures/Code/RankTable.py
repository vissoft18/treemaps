import matplotlib
# Force matplotlib to not use any Xwindows backend.
matplotlib.use('Agg')
import pandas as pd

import Globals


def plot(csv_paths):
    # Read dataframes
    dfs = []
    for path in csv_paths:
        dfs.append(pd.read_csv(path, index_col=0))

    # Initialize counter
    columns = [str(i+1) for i in range(len(dfs[0].index))]
    counter = pd.DataFrame(0, index=dfs[0].index, columns=columns)

    # Count
    for df in dfs:
        for column in df.columns:
            sorted = df.sort_values(column, ascending=False)
            for position, tech in enumerate(sorted.index):
                counter[str(position + 1)][tech] += 1

    counter.index = [Globals.acronyms[i] for i in counter.index]
    counter.to_csv(Globals.plot_subdir + 'table.csv')
