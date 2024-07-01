import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import Product from '../components/Product';

import "./css/search.css"

const Search = () => {
  const [searchParams] = useSearchParams();
  const searchTerm = searchParams.get('term');
  const detailedSearch = searchParams.get('detailedSearch');
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [currentPage,setCurrentPage] = useState(0);

  useEffect(() => {
    const fetchSearchResults = async (page) => {
      setLoading(true);
      try {
        const response = await fetch(`http://localhost:8080/search?searchTerm=${searchTerm}&extendedSearch=${detailedSearch}&page=${page}`);
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

    if (searchTerm) {
      fetchSearchResults(currentPage);
    }
  }, [searchTerm,detailedSearch,currentPage]);

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
          {detailedSearch ?
            "Detaylı arama yapılmasına rağmen böyle bir ürün bulunamadı... Lütfen aradığınız ürünü doğru girdiğinizden emin olun."
            :
            "Böyle bir ürün bulunamadı. İsterseniz arama çubuğunun yanından detaylı aramayı etkinleştirerek daha kapsamlı bir arama yapabilirsiniz."
          }
        </div>
        }
      </div>
  );
};

export default Search;