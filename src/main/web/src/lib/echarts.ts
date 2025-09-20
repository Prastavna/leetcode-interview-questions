import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { BarChart, PieChart } from "echarts/charts";
import {
  DatasetComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
} from "echarts/components";

use([
  CanvasRenderer,
  PieChart,
  BarChart,
  LegendComponent,
  TooltipComponent,
  TitleComponent,
  GridComponent,
  DatasetComponent,
]);
