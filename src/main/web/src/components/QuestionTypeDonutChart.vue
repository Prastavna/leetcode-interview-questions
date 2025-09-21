<template>
  <div class="flex h-full flex-col gap-4 rounded-lg">
    <header class="flex items-start justify-between gap-2">
      <div>
        <h2 class="text-lg font-semibold">Most Asked Question Types</h2>
      </div>
    </header>

    <div class="min-h-[20rem] flex-1">
      <div v-if="!hasData" class="flex h-full items-center justify-center text-sm text-gray-500">
        No question data available for the selected filters.
      </div>
      <VChart
        v-else
        class="h-full w-full"
        :option="chartOptions"
        autoresize
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import VChart from "vue-echarts";
import "../lib/echarts";
import type { Interview } from "../types/Interview";
import { QuestionType, QuestionTypeColors } from "../types/Interview";

type ChartDatum = {
  name: string;
  value: number;
  itemStyle: { color: string };
};

const props = defineProps<{
  interviews: Interview[];
}>();

const typeLabels = Object.entries(QuestionType) as [keyof typeof QuestionType, string][];

const chartData = computed<ChartDatum[]>(() => {
  const counts = new Map<keyof typeof QuestionType, number>();

  for (const [key] of typeLabels) {
    counts.set(key, 0);
  }

  for (const interview of props.interviews ?? []) {
    for (const round of interview.rounds ?? []) {
      for (const question of round.questions ?? []) {
        if (!question?.type) continue;
        const current = counts.get(question.type) ?? 0;
        counts.set(question.type, current + 1);
      }
    }
  }

  return typeLabels
    .map(([key, label]) => ({
      name: label,
      value: counts.get(key) ?? 0,
      itemStyle: { color: QuestionTypeColors[key] ?? "#cccccc" },
    }))
    .filter((item) => item.value > 0)
    .sort((a, b) => b.value - a.value);
});

const hasData = computed(() => chartData.value.length > 0);

const chartOptions = computed(() => ({
  renderer: 'svg',
  tooltip: {
    trigger: "item",
    formatter: ({ name, value, percent }: any) =>
      `${name}: ${value} (${percent}%)`,
  },
  legend: {
    type: "scroll",
    top: 0,
  },
  series: [
    {
      name: "Question Types",
      type: "pie",
      radius: ["45%", "70%"],
      center: ["40%", "50%"],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 8,
        borderColor: "#fff",
        borderWidth: 0,
      },
      label: {
        show: true,
        textStyle: {
          textBorderColor : 'transparent'
        },
        formatter: "{b}: {c} ({d}%)",
      },
      emphasis: {
        scale: true,
        scaleSize: 12,
      },
      labelLine: {
        show: true,
        length: 10,
        length2: 10,
      },
      data: chartData.value,
    },
  ],
}));
</script>
