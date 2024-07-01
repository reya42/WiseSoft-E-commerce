import React from 'react'
import { Link } from 'react-router-dom'


const Brand = ({brand, showDescription = false}) => {

  if (brand) {
    const chunk = (
      <>
        {brand.brandImage ? 
          <img src={`http://localhost:8080/images/${brand.brandImage.imageId}`} alt="" />
        :
          <div></div>
        }
          <div className="single-brand-detail">
            {brand.brandDetail}
          </div>
          {
            showDescription && 
            (
              <div className='single-brand-description'>
                {brand.brandDescription}
              </div>
            )
          }
      </>
    )
    return (
      showDescription ? 
      <div className="brand-container detailed-page">
        {chunk}
      </div>
      :
      <Link className="brand-container" to={`/brands/${brand.brandLink}`}>
        {chunk}
      </Link>
      
    )
  }
  else return null;
}

export default Brand