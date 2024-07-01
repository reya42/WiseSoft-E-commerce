import React, { useState, useEffect } from 'react';
import Product from '../components/Product';

import "./css/search.css"

const Products = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [currentPage,setCurrentPage] = useState(0);

  useEffect(() => {
    const fetchProducts = async (page) => {
      setLoading(true);
      try {
        const response = await fetch(`http://localhost:8080/products?page=${page}`);
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        const data = await response.json();
        setProducts(data);
      } 
      catch (error) {
        console.error('Error fetching search results:', error);
      } 
      finally {
        setLoading(false);
      }
    };

    fetchProducts(currentPage);
  }, [currentPage]);

  const handlePage = (nextPage) =>
  {
    if (!loading) {
      if (nextPage && !products.last) {
        setCurrentPage(currentPage+1);
      }
      else if (!nextPage && !products.first) 
      {
        setCurrentPage(currentPage-1);
      }
    }
  } 
  
  if (loading) {
    return <div>Loading...</div>;
  }

  return (
      <div className="whole-search-container">
        <div className="all-products">
            Tüm Ürünler
        </div>
        {
          products.content && products.content.length > 0?
            <>
              {
                products.content.reduce((resultArray, item, index) => {
                  const chunkIndex = Math.floor(index / 6)

                  if (!resultArray[chunkIndex]) {
                      resultArray[chunkIndex] = []
                  }

                  resultArray[chunkIndex].push(
                      <Product product={item} theme={true} width='14%'/>
                  )

                  return resultArray}, []).map((dataChunk, index) => 
                  (
                    <div className='products-chunk'>
                        {dataChunk}
                    </div>
                  )
                ) 
              }
              {
                products.totalPages > 1 && 
                (
                  <div className='search-page-page-changer-container'>
                    <div className={`search-page-change-page${products.first?"":" active"}`} onClick={() => handlePage(false)}>
                      {"<"}
                    </div>
                    <div className="search-page-page-counter">
                      {currentPage}
                    </div>
                    <div className={`search-page-change-page${products.last?"":" active"}`} onClick={() => handlePage(true)}>
                      {">"}
                    </div>
                  </div>
                )
              }
            </>
        :
        <div className='search-error'>
          Database'de Herhangi bir ürün bulunamadı
        </div>
        }
      </div>
  );
};

export default Products;