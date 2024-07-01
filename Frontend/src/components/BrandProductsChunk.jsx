import React from 'react'
import Product from './Product'

const BrandProductChunk = ({products,theme}) => {
  return (
    <div className='products-container'>
      {
        products.map((product, productIndex) => (
            <Product product={product} theme={false} height='60%' width='16.6%'/>
        ))
      }
    </div>
  )
}

export default BrandProductChunk