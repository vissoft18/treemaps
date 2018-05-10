import matplotlib
# Force matplotlib to not use any Xwindows backend.
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

import Globals


def plot(matrix, dataset_ids, technique_ids, shared_cm=True, cell_text=False, invert_colormap=False, title=None, show=False):
    fig = plt.figure()
    ax = fig.add_subplot(111)

    colormap = plt.cm.viridis_r if invert_colormap else plt.cm.viridis
    if shared_cm:
        # All column share same colormap range
        mat = ax.matshow(matrix, cmap=colormap)
        if cell_text is False:
            fig.colorbar(mat, orientation="horizontal", pad=0.1)

    else:
        # The colormap range is independent for each column
        for col in range(matrix.shape[1]):
            # Make all rows invalid (fill with 1) except the target one (col)
            m = np.ones_like(matrix)
            m[:, col] = 0
            masked = np.ma.masked_array(matrix, m)
            ax.matshow(masked, cmap=colormap)

    # Ticks, labels and grids
    ax.set_xticklabels(dataset_ids, rotation='vertical')
    ax.set_xticks(range(len(dataset_ids)), minor=False)
    ax.set_yticklabels([Globals.acronyms[t] for t in technique_ids])
    ax.set_yticks(range(len(technique_ids)), minor=False)
    ax.set_xticks([x - 0.5 for x in plt.gca().get_xticks()][1:], minor=True)
    ax.set_yticks([y - 0.5 for y in plt.gca().get_yticks()][1:], minor=True)
    plt.grid(which='minor', color='#999999', linestyle='-', linewidth=1)
    ax.tick_params(axis=u'both', which=u'both', length=0)

    x_start = 0.0
    x_end = len(dataset_ids)
    y_start = 0.0
    y_end = len(technique_ids)

    # Add the text
    if cell_text:
        jump_x = (x_end - x_start) / (2.0 * len(dataset_ids))
        jump_y = (y_end - y_start) / (2.0 * len(technique_ids))
        x_positions = np.linspace(start=x_start - 0.5, stop=x_end - 0.5, num=len(dataset_ids), endpoint=False)
        y_positions = np.linspace(start=y_start - 0.5, stop=y_end - 0.5, num=len(technique_ids), endpoint=False)

        for y_index, y in enumerate(y_positions):
            for x_index, x in enumerate(x_positions):
                label = "{0:.3f}".format(matrix[y_index][x_index]).lstrip('0')
                text_x = x + jump_x
                text_y = y + jump_y
                ax.text(text_x, text_y, label, color='black', ha='center', va='center', fontsize=5)

    fig.tight_layout()

    if title is not None:
        # ax.text(x_end / 2, y_end * 1.2, title, color='black', ha='center', va='center', fontsize=12)
        ax.set_xlabel(title)

        csv_name = Globals.plot_subdir + title.replace(' ', '').lower() + '.csv'
        save_as_cvs(matrix, dataset_ids, technique_ids, csv_name)

        fig_name = title.replace(' ', '').lower()
        fig_name += '-S' if shared_cm else '-I'
        fig_name += '-T' if cell_text else '-NT'
        fig_name += '.png'
        fig.savefig(Globals.plot_subdir + fig_name, dpi=500)

    if show:
        plt.show()


def save_as_cvs(matrix, dataset_ids, technique_ids, filename):
    df_dict = dict(zip(dataset_ids, np.array(matrix).transpose()))
    df = pd.DataFrame(data=df_dict, index=technique_ids)
    df.to_csv(filename)
