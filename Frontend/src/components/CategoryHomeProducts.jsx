import React from 'react'
import Product from "./Product"
const CategoryHomeProducts = ({products,theme}) => {
  return (
    <div className="products-container">
      {
        products.content.map((product,productIndex) =>(
          <Product product={product} theme={theme} height={"75%"} width={"15%"}/>
        ))
      }
    </div>
  )
}

export default CategoryHomeProducts