import sys
import os

import Globals
import Correlation
import UnavoidableEnvelope
import UnavoidableMovement
import DeltaMetrics
import AspectRatio
import MeanBoxplot
import RankTable
import StarGlyph

action = sys.argv[1]

# Create dir to store plots
Globals.plot_subdir = 'plots/' + action + '/'
os.makedirs(Globals.plot_subdir, exist_ok=True)

# Choose which vis to create
# Correlation based
if action == 'correlation-scatter':
    dataset_id = sys.argv[2]
    Correlation.scatter(dataset_id)

elif action == 'correlation-matrix':
    dataset_ids = sys.argv[2:]
    Correlation.pearson_matrix(dataset_ids)

# Unavoidable movement
elif action == 'unavoidable-envelope':
    UnavoidableEnvelope.unavoidable_envelope()

elif action == 'unavoidable-boxplots':
    dataset_id = sys.argv[2]
    UnavoidableMovement.plot_time_boxplot(dataset_id)

elif action == 'unavoidable-matrix':
    dataset_ids = sys.argv[2:]
    print(dataset_ids)
    UnavoidableMovement.unavoidable_matrix(dataset_ids)

# Delta vis and delta data combination
elif action == 'delta-ratio-matrix':
    dataset_ids = sys.argv[2:]
    DeltaMetrics.delta_ratio_matrix(dataset_ids)

elif action == 'delta-ratio-boxplots':
    dataset_id = sys.argv[2]
    DeltaMetrics.delta_ratio_boxplots(dataset_id)

elif action == 'delta-diff-matrix':
    dataset_ids = sys.argv[2:]
    DeltaMetrics.delta_diff_matrix(dataset_ids)

elif action == 'delta-diff-boxplots':
    dataset_id = sys.argv[2]
    DeltaMetrics.delta_diff_boxplots(dataset_id)

# Aspect Ratio
elif action == 'ar-boxplots':
    dataset_id = sys.argv[2]
    AspectRatio.plot_time_boxplot(dataset_id)

elif action == 'ar-matrix':
    dataset_ids = sys.argv[2:]
    AspectRatio.plot_ar_matrix(dataset_ids)

# Mean boxplot of all (3) metric
elif action == 'mean-boxplots':
    dataset_id = sys.argv[2]
    MeanBoxplot.plot_mean_boxplot(dataset_id)

elif action == 'rank-table':
    csv_paths = sys.argv[2:]
    RankTable.plot(csv_paths)

elif action == 'star-glyph':
    csv_paths = sys.argv[2:]
    StarGlyph.plot(csv_paths)

else:
    print('Invalid command. See the readme file.')