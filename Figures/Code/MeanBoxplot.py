import Metrics
import Parser
import TimeBoxplot
import Globals

technique_list = Parser.list_techniques()


def plot_mean_boxplot(dataset_id):
    data = []
    for i, technique_id in enumerate(technique_list):
        print(Globals.acronyms[technique_id], end=' ', flush=True)
        technique_data = []
        history = Parser.parse_rectangles(technique_id, dataset_id)
        for revision in range(len(history) - 1):
            delta_vis = Metrics.compute_delta_vis(history[revision], history[revision + 1])
            delta_data = Metrics.compute_delta_data(history[revision], history[revision + 1])
            un_mov = Metrics.compute_unavoidable_movement(history[revision], history[revision + 1])

            ratios = (1 - delta_vis) / (1 - delta_data)
            diffs = 1 - abs(delta_vis - delta_data)
            unavoidable = 1 - (delta_vis - un_mov)

            mean = (ratios + diffs + unavoidable) / 3
            technique_data.append(mean)
        data.append(technique_data)

    TimeBoxplot.plot(data, technique_list,
                     title='Mean - ' + dataset_id)

    TimeBoxplot.plot(data, technique_list,
                     median_sorted=True,
                     title='Mean - ' + dataset_id)
