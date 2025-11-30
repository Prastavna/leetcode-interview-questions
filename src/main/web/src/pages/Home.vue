<template>
  <div class="flex flex-col gap-6 w-5/6 mx-auto">
    <div class="flex gap-3 px-4 pt-4 flex-col sm:flex-row items-center justify-center">
      <div class="text-base font-semibold text-gray-800 dark:text-white">Leetcode Interview Questions</div>
      <div class="flex items-center gap-3">
        <UButton
          :icon="theme === 'auto' ? 'i-lucide-monitor' : effectiveTheme === 'dark' ? 'i-lucide-sun' : 'i-lucide-moon'"
          color="gray"
          variant="ghost"
          size="sm"
          @click="toggleTheme"
          :aria-label="theme === 'auto' ? 'Auto theme (follows system)' : effectiveTheme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'"
        />
        <a
          class="inline-flex items-center gap-2 text-sm text-primary hover:underline"
          href="https://github.com/prastavna/leetcode-interview-questions"
          target="_blank"
          rel="noopener noreferrer"
        >
          <img
            alt="Star this repo on GitHub"
            src="https://img.shields.io/github/stars/prastavna/leetcode-interview-questions?style=social"
            class="h-6"
          />
        </a>
      </div>
    </div>

    <div v-if="!isLoading" class="grid lg:grid-cols-2 pt-4">
      <QuestionTypeDonutChart :interviews="filteredInterviews" />
      <CompanyQuestionTypeStackedBarChart :interviews="filteredInterviews" />
    </div>

    <TableFilters
      v-model="filters"
      :question-type-options="questionTypeItems"
      :min-yoe="YOE_MIN"
      :max-yoe="YOE_MAX"
      :min-date="MIN_DATE"
      :max-date="MAX_DATE"
    />

    <Table :data="filteredInterviews" :is-loading="isLoading" :error="error" />

    <footer class="mt-6 border-t border-gray-200 pt-6 pb-10 text-center text-sm text-gray-600">
      <span>
        Compensation insights generated from Leetcode Discussions:
        <a
          href="https://leetcode-compensation.prastavna.com/"
          target="_blank"
          rel="noopener noreferrer"
          class="text-primary font-semibold hover:underline"
        >
          Leetcode Compensation
        </a>
      </span>
    </footer>
  </div>
</template>

<script setup lang="ts">
import Table from "../components/Table.vue";
import TableFilters from "../components/TableFilters.vue";
import QuestionTypeDonutChart from "../components/QuestionTypeDonutChart.vue";
import CompanyQuestionTypeStackedBarChart from "../components/CompanyQuestionTypeStackedBarChart.vue";
import { useInterviewsData } from "../lib/interviews/useInterviewsData";
import { useInterviewFilters } from "../lib/interviews/useInterviewFilters";
import { useTheme } from "../utils/useTheme";

const { interviews, isLoading, error } = useInterviewsData();
const {
	filters,
	filteredInterviews,
	questionTypeItems,
	metadata: { yoeBounds, dateBounds },
} = useInterviewFilters(interviews);

const { theme, effectiveTheme, toggleTheme } = useTheme();

const YOE_MIN = yoeBounds.min;
const YOE_MAX = yoeBounds.max;
const MIN_DATE = dateBounds.min;
const MAX_DATE = dateBounds.max;
</script>
