#pragma once

namespace presents
{	
	class PresentsError : public std::runtime_error
	{
	public:
		PresentsError(const std::string& message) : std::runtime_error(message)
        {}
		
		virtual ~PresentsError() throw()
		{}
	};
}