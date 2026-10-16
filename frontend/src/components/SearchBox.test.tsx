import { cleanup, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { SearchBox } from './SearchBox'

describe('SearchBox', () => {
  afterEach(cleanup)

  it('searches with the button', async () => {
    const onSearch = vi.fn()
    render(<SearchBox onSearch={onSearch} />)

    await userEvent.type(screen.getByRole('searchbox', { name: 'Search' }), '  dell ')
    await userEvent.click(screen.getByRole('button', { name: 'Search' }))

    expect(onSearch).toHaveBeenCalledWith('dell')
  })

  it('searches with Enter', async () => {
    const onSearch = vi.fn()
    render(<SearchBox onSearch={onSearch} />)

    await userEvent.type(screen.getByRole('searchbox', { name: 'Search' }), 'lamp{Enter}')

    expect(onSearch).toHaveBeenCalledWith('lamp')
  })
})
