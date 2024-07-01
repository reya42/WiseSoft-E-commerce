import React from 'react'

import { addtocarticon,inspectproducticon,white_addtocarticon,white_inspectproducticon } from '../assets'
import "./css/product.css"

const Product = ({product,includeBrand = true,theme, width = "23.96%", height = "60%"}) => {
  return (
    <div className={`product-container ${theme?"theme":""}`} style={{width:width, height:height}}>
      { product.images && (
          product.images.length >0 ?
          <img src={`http://localhost:8080/images/${product.images[0].imageId}`} alt="" className={`product-image ${theme?"theme":""}`}/>
          :
          <div className={`product-img-holder ${theme?"theme":""}`}>
            Bu Ürünün Görseli Bulunamadı
          </div>
        )
      }
      {includeBrand && product.brand && (
        <div className={`product-brand ${theme ? "theme" : ""}`}>
          {product.brand.brandName}
        </div>
      )}
      <div className={`product-name ${theme?"theme":""}`}>
        {product.productName}
      </div>
      <div className='product-prices'>
          <div className={`product-price${product.productDiscountedPrice !== -1 ? " discounted":""} ${theme?"theme":""}`}>
              {product.productSellPrice} TL
          </div>
          {product.productDiscountedPrice !== -1 && (
          <div className={`product-discounted-price ${theme?"theme":""}`}>
              {product.productDiscountedPrice} TL
          </div>
              )}
      </div>
      <div className={`product-transactions ${theme?"theme":""}`}>
          <a href={`http://localhost:3000/products/${product.productLink}`}>
              Inspect
              <img src={theme ? inspectproducticon : white_inspectproducticon} alt="" style={{marginLeft:"5px"}}/>
          </a>
          <a href='#'>
              <img src={theme ? addtocarticon : white_addtocarticon} alt="" />
              Add To Cart
          </a>
      </div>
    </div>
  )
}

export default Product