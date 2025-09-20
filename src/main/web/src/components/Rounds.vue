<script setup lang="ts">
import { computed, withDefaults, defineProps } from "vue";
import type { Round } from "../types/Interview";
import { QuestionType } from "../types/Interview";

const props = withDefaults(
  defineProps<{
    rounds?: Round[];
  }>(),
  {
    rounds: () => [],
  },
);

const normalizedRounds = computed(() => props.rounds ?? []);

const getQuestionLabel = (type: Round["questions"][number]["type"] | undefined) => {
  if (!type) return "";
  return QuestionType[type] ?? type;
};
</script>

<template>
  <div class="space-y-4">
    <p v-if="!normalizedRounds.length" class="text-sm text-gray-500">No rounds provided.</p>
    <div v-else class="grid gap-4 md:grid-cols-2">
      <div
        v-for="(round, index) in normalizedRounds"
        :key="round?.id ?? index"
        class="rounded-lg border border-accented bg-white/5 p-4 shadow-sm"
      >
        <header class="mb-3 text-sm font-semibold uppercase tracking-wide text-primary">
          Round {{ index + 1 }}
        </header>
        <ul class="space-y-3">
          <li
            v-for="(question, qIndex) in round?.questions ?? []"
            :key="question?.id ?? qIndex"
            class="space-y-1"
          >
            <p class="text-sm font-medium text-gray-700">
              {{ getQuestionLabel(question?.type) }}
            </p>
            <p class="text-sm leading-relaxed text-gray-600 whitespace-pre-line">
              {{ question?.content }}
            </p>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>
