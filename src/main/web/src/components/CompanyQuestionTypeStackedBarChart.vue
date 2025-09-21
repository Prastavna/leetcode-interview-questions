<template>
  <div class="flex h-full flex-col gap-4 rounded-lg">
    <header class="flex items-start justify-between gap-2">
      <div>
        <h2 class="text-lg font-semibold">Question Type Breakdown by Company</h2>
      </div>
    </header>

    <div class="min-h-[20rem] flex-1">
      <div v-if="!hasData" class="flex h-full items-center justify-center text-sm text-gray-500">
        Not enough question data to build this chart.
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

type QuestionTypeKey = keyof typeof QuestionType;

const props = defineProps<{
  interviews: Interview[];
}>();

const typeEntries = Object.entries(QuestionType) as [QuestionTypeKey, string][];

const aggregated = computed(() => {
  const perCompany = new Map<string, Map<QuestionTypeKey, number>>();

  for (const interview of props.interviews ?? []) {
    const companyName = interview.company?.trim() || "Unknown";
    if (!perCompany.has(companyName)) {
      perCompany.set(companyName, new Map<QuestionTypeKey, number>());
    }

    const companyCounts = perCompany.get(companyName)!;

    for (const [key] of typeEntries) {
      if (!companyCounts.has(key)) companyCounts.set(key, 0);
    }

    for (const round of interview.rounds ?? []) {
      for (const question of round.questions ?? []) {
        if (!question?.type) continue;
        const current = companyCounts.get(question.type) ?? 0;
        companyCounts.set(question.type, current + 1);
      }
    }
  }

  const companiesWithTotals = Array.from(perCompany.entries())
    .map(([name, counts]) => {
      const total = Array.from(counts.values()).reduce((sum, value) => sum + value, 0);
      return { name, counts, total };
    })
    .filter(({ total }) => total > 0)
    .sort((a, b) => b.total - a.total)
    .slice(0, 10);

  const companyNames = companiesWithTotals.map(({ name }) => name);

  const activeTypeEntries = typeEntries.filter(([key]) =>
    companiesWithTotals.some(({ counts }) => (counts.get(key) ?? 0) > 0),
  );

  const series = activeTypeEntries.map(([key, label]) => {
    const color = QuestionTypeColors[key] ?? "#cccccc";
    return {
      name: label,
      type: "bar" as const,
      stack: "total",
      emphasis: { focus: "series" as const },
      itemStyle: { color },
      data: companyNames.map((_, index) => companiesWithTotals[index]?.counts.get(key) ?? 0),
    };
  });

  return { companyNames, series };
});

const hasData = computed(() => aggregated.value.companyNames.length > 0 && aggregated.value.series.length > 0);

const shouldRotateLabels = computed(() =>
  aggregated.value.companyNames.some((company) => company.length > 12),
);

const chartOptions = computed(() => ({
  tooltip: {
    trigger: "axis",
    axisPointer: { type: "shadow" },
  },
  legend: {
    type: "scroll",
    top: 0,
  },
  grid: {
    left: "6%",
    right: "4%",
    bottom: "8%",
    containLabel: true,
  },
  xAxis: {
    type: "category",
    data: aggregated.value.companyNames,
    axisLabel: {
      interval: 0,
      rotate: shouldRotateLabels.value ? 30 : 0,
      formatter: (value: string) => value,
    },
  },
  yAxis: {
    type: "value",
    name: "Questions",
  },
  series: aggregated.value.series,
}));
</script>
