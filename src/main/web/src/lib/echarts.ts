import { use } from "echarts/core";
import { SVGRenderer } from "echarts/renderers";
import { BarChart, PieChart } from "echarts/charts";
import {
  DatasetComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
} from "echarts/components";

use([
  SVGRenderer,
  PieChart,
  BarChart,
  LegendComponent,
  TooltipComponent,
  TitleComponent,
  GridComponent,
  DatasetComponent,
]);
