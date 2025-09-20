<script setup lang="ts">
import { computed, watch } from "vue";

export type TableFiltersState = {
  search: string;
  questionTypes: string[];
  yoeRange: [number, number];
  dateRange: {
    from: string | null;
    to: string | null;
  };
};

const props = withDefaults(
  defineProps<{
    modelValue: TableFiltersState;
    questionTypeOptions?: { label: string; value: string }[];
    minYoe?: number;
    maxYoe?: number;
    minDate?: string | null;
    maxDate?: string | null;
  }>(),
  {
    questionTypeOptions: () => [],
    minYoe: 0,
    maxYoe: 0,
    minDate: null,
    maxDate: null,
  },
);

const emit = defineEmits<{ "update:modelValue": [TableFiltersState] }>();

const filters = computed<TableFiltersState>({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", {
    ...value,
    questionTypes: Array.isArray(value.questionTypes) ? value.questionTypes : [],
    yoeRange: value.yoeRange,
    dateRange: {
      from: value.dateRange?.from ?? null,
      to: value.dateRange?.to ?? null,
    },
  }),
});

const search = computed({
  get: () => filters.value.search,
  set: (value: string) => {
    filters.value = { ...filters.value, search: value };
  },
});

const questionTypes = computed({
  get: () => filters.value.questionTypes,
  set: (value: string[] | string) => {
    const arrayValue = Array.isArray(value) ? value : value ? [value] : [];
    filters.value = { ...filters.value, questionTypes: arrayValue };
  },
});

const yoeRange = computed({
  get: () => filters.value.yoeRange,
  set: (value: number[] | number) => {
    if (!Array.isArray(value)) return;
    if (value.length < 2) {
      filters.value = { ...filters.value, yoeRange: [value[0] ?? props.minYoe, value[0] ?? props.maxYoe] as [number, number] };
      return;
    }
    filters.value = { ...filters.value, yoeRange: [value[0], value[1]] as [number, number] };
  },
});

const dateFrom = computed({
  get: () => filters.value.dateRange.from,
  set: (value: string | null) => {
    filters.value = {
      ...filters.value,
      dateRange: {
        ...filters.value.dateRange,
        from: value && value.length ? value : null,
      },
    };
  },
});

const dateTo = computed({
  get: () => filters.value.dateRange.to,
  set: (value: string | null) => {
    filters.value = {
      ...filters.value,
      dateRange: {
        ...filters.value.dateRange,
        to: value && value.length ? value : null,
      },
    };
  },
});

watch(
  () => [props.minYoe, props.maxYoe],
  ([min, max]) => {
    if (min === undefined || max === undefined) return;
    const clampedMin = Math.min(min, max);
    const clampedMax = Math.max(min, max);
    const [currentMin, currentMax] = filters.value.yoeRange ?? [clampedMin, clampedMax];
    const nextMin = Math.min(Math.max(currentMin, clampedMin), clampedMax);
    const nextMax = Math.min(Math.max(currentMax, clampedMin), clampedMax);
    if (currentMin !== nextMin || currentMax !== nextMax) {
      filters.value = {
        ...filters.value,
        yoeRange: [nextMin, nextMax] as [number, number],
      };
    }
  },
  { immediate: true },
);
</script>

<template>
  <div class="flex w-full flex-col justify-center gap-4 px-4 md:flex-row md:flex-wrap md:items-end md:gap-6 lg:flex-nowrap">
    <UInput
      v-model="search"
      placeholder="Search company or role"
      icon="i-lucide-search"
      class="w-full md:w-64 lg:w-72"
    />

    <USelectMenu
      v-model="questionTypes"
      multiple
      :items="props.questionTypeOptions"
      placeholder="Question types"
      class="w-full md:w-64 lg:w-72"
    />

    <div class="flex flex-col gap-2 w-full relative -mb-2 md:w-64 lg:w-72">
      <USlider
        v-model="yoeRange"
        :min="props.minYoe"
        :max="props.maxYoe"
        :step="1"
        tooltip
      />
      <div class="flex justify-between text-xs text-gray-600">
        <span>{{ yoeRange[0] }}</span>
        <span>{{ yoeRange[1] }}</span>
      </div>
    </div>

    <div class="flex flex-col gap-2 w-full md:max-w-[320px]">
      <div class="grid grid-cols-2 gap-3">
        <UInput
          v-model="dateFrom"
          type="date"
          placeholder="From"
          :max="dateTo || props.maxDate || undefined"
          :min="props.minDate || undefined"
        />
        <UInput
          v-model="dateTo"
          type="date"
          placeholder="To"
          :min="dateFrom || props.minDate || undefined"
          :max="props.maxDate || undefined"
        />
      </div>
    </div>
  </div>
</template>
