import React from 'react'

const BrandDetailChunk = ({brand}) => {
  return (
    <>
      {
        brand.brandImage && <img src={`http://localhost:8080/images/${brand.brandImage.imageId}`} alt="" />
      }
      <div className="detail">
        {brand.brandDetail}
      </div>
    </>
  )
}

export default BrandDetailChunk