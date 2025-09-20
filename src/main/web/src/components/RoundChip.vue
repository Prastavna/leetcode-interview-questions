<script setup lang="ts">
import { computed } from "vue";
import type { Round } from "../types/Interview";
import { QuestionType } from "../types/Interview";

const props = defineProps<{
  round?: Round | null;
}>();

const questionLabels = computed(() => {
  const labels = new Set<string>();
  const round = props.round;
  if (!round?.questions?.length) return labels;

  for (const question of round.questions) {
    if (!question) continue;
    const typeKey = question.type as keyof typeof QuestionType | undefined;
    const label = typeKey && QuestionType[typeKey] ? QuestionType[typeKey] : question.type;
    if (label) labels.add(label as string);
  }

  return labels;
});

const formattedLabel = computed(() => {
  const segments = Array.from(questionLabels.value);
  return segments.length ? segments.join(" | ") : "—";
});
</script>

<template>
  <UBadge variant="subtle" size="xs" color="primary" class="uppercase tracking-wide">
    {{ formattedLabel }}
  </UBadge>
</template>
