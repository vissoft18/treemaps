import matplotlib
# Force matplotlib to not use any Xwindows backend.
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from adjustText import adjust_text
import pandas as pd
import Globals


def plot(csv_paths):
    fig = plt.figure(figsize=(9, 9))
    brewer = ['#a6cee3', '#1f78b4', '#b2df8a', '#33a02c', '#fb9a99', '#e31a1c', '#fdbf6f', '#ff7f00', '#cab2d6', '#6a3d9a','#919114', '#b15928']

    # Read dataframes
    ar_df = pd.read_csv(csv_paths[0], index_col=0)

    stab_dfs = []
    for path in csv_paths[1:]:
        stab_dfs.append(pd.read_csv(path, index_col=0))

    stab_df = pd.concat(stab_dfs)
    stab_df = stab_df.groupby(level=0).mean()
    texts = []
    for i, tech in enumerate(stab_df.index):

        ar_mean = (ar_df.loc[[tech],:]).mean(axis=1)
        st_mean = (stab_df.loc[[tech],:]).mean(axis=1)
        for dataset in stab_df.columns:
            ar_ob = ar_df[dataset][tech]
            st_ob = stab_df[dataset][tech]
            y_line = [ar_mean, ar_ob]
            x_line = [st_mean, st_ob]
            plt.plot(x_line, y_line, c=brewer[i], alpha=0.4, zorder=1)

        plt.scatter(st_mean, ar_mean, s=80, c=brewer[i], label=tech, linewidth=2, zorder=10)
        t = plt.text(st_mean, ar_mean, Globals.acronyms[tech], ha='center', va='center', zorder=11,
                     fontsize=14, fontweight='bold')
        texts.append(t)

    # adjust_text(texts)
    adjust_text(texts, force_points=1.0, force_text=1.0, expand_points=(1, 1), expand_text=(1, 1))
    plt.savefig(Globals.plot_subdir + 'star.png')



    # Initialize counter
    # columns = [str(i+1) for i in range(len(dfs[0].index))]
    # counter = pd.DataFrame(0, index=dfs[0].index, columns=columns)
    #
    # # Count
    # for df in dfs:
    #     for column in df.columns:
    #         sorted = df.sort_values(column, ascending=False)
    #         for position, tech in enumerate(sorted.index):
    #             counter[str(position + 1)][tech] += 1
    #
    # counter.index = [Globals.acronyms[i] for i in counter.index]
    # counter.to_csv(Globals.plot_subdir + 'table.csv')