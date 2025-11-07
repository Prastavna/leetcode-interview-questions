<template>
  <div class="flex flex-col gap-6 w-5/6 mx-auto">
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
  </div>
</template>

<script setup lang="ts">
import Table from "../components/Table.vue";
import TableFilters from "../components/TableFilters.vue";
import QuestionTypeDonutChart from "../components/QuestionTypeDonutChart.vue";
import CompanyQuestionTypeStackedBarChart from "../components/CompanyQuestionTypeStackedBarChart.vue";
import { useInterviewsData } from "../lib/interviews/useInterviewsData";
import { useInterviewFilters } from "../lib/interviews/useInterviewFilters";

const { interviews, isLoading, error } = useInterviewsData();
const {
	filters,
	filteredInterviews,
	questionTypeItems,
	metadata: { yoeBounds, dateBounds },
} = useInterviewFilters(interviews);

const YOE_MIN = yoeBounds.min;
const YOE_MAX = yoeBounds.max;
const MIN_DATE = dateBounds.min;
const MAX_DATE = dateBounds.max;
</script>
