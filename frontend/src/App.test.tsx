import { render, screen } from '@testing-library/react'
import App from './App'
import { test, expect } from 'vitest'

test('renders daisyui button', () => {
  render(<App />)
  const button = screen.getByText(/Test/i)
  expect(button.className).toContain('btn')
  expect(button.className).toContain('btn-primary')
})
