<script setup lang="ts">
import { computed, defineProps, withDefaults } from "vue";

const props = withDefaults(
  defineProps<{
    text?: string;
    segments?: string[];
  }>(),
  {
    text: "",
    segments: () => [],
  },
);

const segmentValues = computed(() =>
  (props.segments ?? [])
    .map((segment) => segment?.trim())
    .filter((segment): segment is string => Boolean(segment && segment.length > 0)),
);

const hasSegments = computed(() => segmentValues.value.length > 0);
const fallbackText = computed(() => props.text || "—");
</script>

<template>
  <span
    class="inline-flex items-center rounded-full border border-accented bg-primary/5 px-2.5 py-1 text-xs font-medium text-primary"
  >
    <template v-if="hasSegments">
      <span
        v-for="(segment, index) in segmentValues"
        :key="`${segment}-${index}`"
        :class="[
          'px-1 first:pl-0 last:pr-0 text-[0.7rem] uppercase tracking-wide',
          index > 0 ? 'border-l border-accented/60' : '',
        ]"
      >
        {{ segment }}
      </span>
    </template>
    <template v-else>
      {{ fallbackText }}
    </template>
  </span>
</template>
