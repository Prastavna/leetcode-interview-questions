export const QuestionType = {
  APTITUDE: 'Aptitude',
  DEBUGGING: 'Debugging',
  DSA: 'DSA',
  GD: 'Group Discussion',
  HLD: 'High Level Design',
  HM: 'Hiring Manager',
  HR: 'HR',
  LANGUAGE_SPECIFIC: 'Language Specific',
  LLD: 'Low Level Design',
  MACHINE_CODING: 'Machine Coding',
  PROJECT_DISCUSSION: 'Project Discussion',
  TAKE_HOME_ASSIGNMENT: 'Take Home Assignment',
  TECHNICAL: 'Technical',
  SYSTEM_DESIGN: 'System Design'
} as const

export type Question = {
  id: string,
  type: keyof typeof QuestionType,
  content: string
}

export type Round = {
  id: string,
  questions: Question[]
}

export type Interview = {
  id: string
  leetcode_id: string
  company: string
  role: string
  yoe: number
  rounds: Round[]
  date: string
}
