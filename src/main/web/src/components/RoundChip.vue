<script setup lang="ts">
import { computed } from "vue";
import type { Round } from "../types/Interview";
import { QuestionType, QuestionTypeColors } from "../types/Interview";

const props = defineProps<{
  round?: Round | null;
}>();

const isHexColorDark = (hex: string) => {
  const normalized = hex.replace("#", "");
  if (normalized.length !== 6) return false;
  const r = Number.parseInt(normalized.slice(0, 2), 16);
  const g = Number.parseInt(normalized.slice(2, 4), 16);
  const b = Number.parseInt(normalized.slice(4, 6), 16);
  if ([r, g, b].some((channel) => Number.isNaN(channel))) return false;
  const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
  return luminance < 0.6;
};

const questionTypeKeys = computed(() => {
  const keys = new Set<keyof typeof QuestionType>();
  const round = props.round;
  if (!round?.questions?.length) return Array.from(keys);

  for (const question of round.questions) {
    if (!question?.type) continue;
    const typeKey = question.type as keyof typeof QuestionType;
    if (QuestionType[typeKey]) keys.add(typeKey);
  }

  return Array.from(keys);
});

const formattedLabel = computed(() => {
  const segments = questionTypeKeys.value.map((key) => QuestionType[key] ?? key);
  return segments.length ? segments.join(" | ") : "—";
});

const badgeStyle = computed(() => {
  const [firstType] = questionTypeKeys.value;
  if (!firstType) return {};
  const backgroundColor = QuestionTypeColors[firstType];
  if (!backgroundColor) return {};
  const dark = isHexColorDark(backgroundColor);
  return {
    backgroundColor,
    color: dark ? "#ffffff" : "#1f2937",
    borderColor: backgroundColor,
  } as const;
});
</script>

<template>
  <UBadge
    variant="solid"
    size="xs"
    :style="badgeStyle"
    class="uppercase tracking-wide border border-transparent"
  >
    {{ formattedLabel }}
  </UBadge>
</template>
