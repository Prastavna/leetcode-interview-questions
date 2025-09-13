<script setup lang="ts">
import { h, ref, resolveComponent } from 'vue'
import type { TableColumn } from '@nuxt/ui'
import type { Interview } from '../types/Interview'

const UBadge = resolveComponent('UBadge')
const UButton = resolveComponent('UButton')

const data = ref<Interview[]>([
  {
    id: '123',
    leetcode_id: 'abc',
    company: 'Amazon',
    role: 'SDE 1',
    yoe: 2,
    questions: [
      {
        id: '1223',
        type: 'DSA',
        content: 'Some questoinnnn'
      }
    ],
    date: '2025-03-04'
  },
  {
    id: '123a',
    leetcode_id: 'abac',
    company: 'Microsoft',
    role: 'SDE II',
    yoe: 5,
    questions: [
      {
        id: '1223',
        type: 'HLD',
        content: 'Design patebin'
      }
    ],
    date: '2025-03-04'
  }
])

const columns: TableColumn<Interview>[] = [
  {
    id: 'expand',
    cell: ({ row }) =>
      h(UButton, {
        color: 'neutral',
        variant: 'ghost',
        icon: 'i-lucide-chevron-down',
        square: true,
        'aria-label': 'Expand',
        ui: {
          leadingIcon: [
            'transition-transform',
            row.getIsExpanded() ? 'duration-200 rotate-180' : ''
          ]
        },
        onClick: () => row.toggleExpanded()
      })
  },
  {
    accessorKey: 'id',
    header: '#',
    cell: ({ row }) => `${row.getValue('id')}`
  },
  {
    accessorKey: 'company',
    header: 'Company',
  },
  {
    accessorKey: 'role',
    header: 'Role',
  },
  {
    accessorKey: 'yoe',
    header: 'YoE'
  },
  {
    accessorKey: 'questions',
    header: '# of Questions',
    cell: ({ row }) => {
      return row.getValue("questions").length
    }
  },
  {
    accessorKey: 'date',
    header: 'Date',
    cell: ({ row }) => {
      return new Date(row.getValue('date')).toLocaleString('en-US', {
        day: 'numeric',
        month: 'short',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      })
    }
  },
]

const globalFilter = ref('')
const expanded = ref({ 1: true })
</script>

<template>
  <div class="flex flex-col flex-1 w-full">
    <div class="flex px-4 py-3.5 border-b border-accented">
      <UInput v-model="globalFilter" class="max-w-sm" placeholder="Filter..." />
    </div>

    <UTable
      v-model:expanded="expanded"
      ref="table"
      v-model:global-filter="globalFilter"
      :data="data"
      :columns="columns"
    >
        <template #expanded="{ row }">
          <UContainer>
            <div v-for="question in row.getValue('questions')">
              <p>{{ question.type }}</p>
              <p>{{ question.content }}</p>
            </div>
          </UContainer>
        </template>
    </Utable>
  </div>
</template>
